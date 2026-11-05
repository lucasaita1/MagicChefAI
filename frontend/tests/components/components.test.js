import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RobotChef from '@/components/RobotChef.vue'
import RecipeCard from '@/components/RecipeCard.vue'
import RecipeDetail from '@/components/RecipeDetail.vue'
import BaseModal from '@/components/BaseModal.vue'
import PantryPicker from '@/components/PantryPicker.vue'
import PantryItemForm from '@/components/PantryItemForm.vue'
import AppIcon from '@/components/AppIcon.vue'
import { sampleItem, sampleRecipe, flush } from '../helpers'

const global = { stubs: { RouterLink: { template: '<a><slot /></a>' }, teleport: true } }

describe('RobotChef', () => {
  it('reflects the mood in a CSS class and accessible label', () => {
    const wrapper = mount(RobotChef, { props: { mood: 'thinking', size: 100 } })
    expect(wrapper.classes()).toContain('is-thinking')
    expect(wrapper.attributes('aria-label')).toContain('thinking')
    expect(wrapper.attributes('width')).toBe('100')
  })

  it('follows the gaze within limits and covers its eyes when shy', () => {
    const looking = mount(RobotChef, { props: { lookX: 5, lookY: -0.5 } })
    expect(looking.find('.eyes').attributes('style')).toContain('translate(7px, -2px)')

    const shy = mount(RobotChef, { props: { mood: 'shy' } })
    expect(shy.classes()).toContain('is-shy')
    expect(shy.findAll('.eye')).toHaveLength(0)
    expect(shy.findAll('.cover')).toHaveLength(2)
  })

  it('shows smiling eyes when happy', () => {
    expect(mount(RobotChef, { props: { mood: 'happy' } }).findAll('.eye')).toHaveLength(0)
    expect(mount(RobotChef).findAll('.eye')).toHaveLength(2)
  })
})

describe('AppIcon', () => {
  it.each(['book', 'fridge', 'eye', 'eye-off', 'heart', 'carrot', 'cheese', 'snowflake'])('renders %s', (name) => {
    expect(mount(AppIcon, { props: { name } }).find('path, rect, circle').exists()).toBe(true)
  })

  it('falls back to a neutral icon and supports filled style', () => {
    const wrapper = mount(AppIcon, { props: { name: 'unknown', filled: true } })
    expect(wrapper.find('path').exists()).toBe(true)
    expect(wrapper.attributes('fill')).toBe('currentColor')
  })
})

describe('RecipeCard', () => {
  it('renders recipe info and emits open and favorite', async () => {
    const recipe = sampleRecipe({ servings: 1 })
    const wrapper = mount(RecipeCard, { props: { recipe } })

    expect(wrapper.text()).toContain('Omelete do Chef')
    expect(wrapper.text()).toContain('1 porção')
    expect(wrapper.text()).toContain('Fácil')

    await wrapper.find('.fav').trigger('click')
    expect(wrapper.emitted('favorite')[0]).toEqual([recipe])
    expect(wrapper.emitted('open')).toBeUndefined()

    await wrapper.trigger('click')
    expect(wrapper.emitted('open')[0]).toEqual([recipe])
  })
})

describe('RecipeDetail', () => {
  it('lists ingredients, numbered steps and tips, and checks ingredients off', async () => {
    const wrapper = mount(RecipeDetail, { props: { recipe: sampleRecipe() } })

    expect(wrapper.findAll('.steps li')).toHaveLength(2)
    expect(wrapper.text()).toContain('Fogo baixo')
    expect(wrapper.text()).toContain('2 porções')

    const first = wrapper.find('.ingredients li')
    await first.trigger('click')
    expect(first.classes()).toContain('done')
    await first.trigger('click')
    expect(first.classes()).not.toContain('done')
  })

  it('hides optional sections', () => {
    const wrapper = mount(RecipeDetail, { props: { recipe: sampleRecipe({ tips: [], chefComment: null, difficulty: null }) } })
    expect(wrapper.find('.tips').exists()).toBe(false)
    expect(wrapper.find('.quote').exists()).toBe(false)
  })
})

describe('BaseModal', () => {
  it('closes on escape, close button and backdrop click', async () => {
    const wrapper = mount(BaseModal, { props: { title: 'Teste' }, slots: { default: 'conteúdo' }, global, attachTo: document.body })
    expect(wrapper.text()).toContain('conteúdo')
    expect(document.body.style.overflow).toBe('hidden')

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    await wrapper.find('[aria-label="Fechar"]').trigger('click')
    await wrapper.find('.backdrop').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(3)

    wrapper.unmount()
    expect(document.body.style.overflow).toBe('')
  })
})

describe('PantryPicker', () => {
  const items = [sampleItem(), sampleItem({ id: 11, name: 'Arroz', location: 'CUPBOARD', category: 'GRAIN' })]

  it('toggles items and selects whole groups', async () => {
    const wrapper = mount(PantryPicker, { props: { items, selected: [] }, global })

    await wrapper.findAll('.item')[0].trigger('click')
    expect(wrapper.emitted('update:selected')[0]).toEqual([[10]])

    await wrapper.setProps({ selected: [10] })
    await wrapper.findAll('.item')[0].trigger('click')
    expect(wrapper.emitted('update:selected')[1]).toEqual([[]])

    await wrapper.findAll('.link')[1].trigger('click')
    expect(wrapper.emitted('update:selected')[2]).toEqual([[10, 11]])
  })

  it('explains how to fill an empty pantry', () => {
    expect(mount(PantryPicker, { props: { items: [], selected: [] }, global }).text()).toContain('despensa está vazia')
  })
})

describe('PantryItemForm', () => {
  it('submits normalized data and emits done', async () => {
    const submit = vi.fn().mockResolvedValue({})
    const wrapper = mount(PantryItemForm, { props: { submit, defaultLocation: 'CUPBOARD' } })

    await wrapper.find('#item-name').setValue('Feijão')
    await wrapper.find('#item-quantity').setValue('2')
    await wrapper.find('form').trigger('submit')
    await flush()

    expect(submit).toHaveBeenCalledWith(expect.objectContaining({ name: 'Feijão', quantity: 2, location: 'CUPBOARD', expiresAt: null }))
    expect(wrapper.emitted('done')).toHaveLength(1)
  })

  it('shows API errors and prefills when editing', async () => {
    const error = Object.assign(new Error('O nome contém caracteres não permitidos'), { fieldErrors: { name: 'inválido' } })
    const submit = vi.fn().mockRejectedValue(error)
    const wrapper = mount(PantryItemForm, { props: { submit, item: sampleItem() } })

    expect(wrapper.find('#item-name').element.value).toBe('Leite')
    expect(wrapper.text()).toContain('Salvar alterações')

    await wrapper.find('form').trigger('submit')
    await flush()

    expect(wrapper.text()).toContain('caracteres não permitidos')
    expect(wrapper.text()).toContain('inválido')
    expect(wrapper.emitted('done')).toBeUndefined()
  })
})
